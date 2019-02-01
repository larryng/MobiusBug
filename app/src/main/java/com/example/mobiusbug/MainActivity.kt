package com.example.mobiusbug

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxConnectables
import com.spotify.mobius.rx2.RxEventSources
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.disposables.SerialDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class Model(val count: Long = 0L)
object Event
object Effect

class MainActivity : AppCompatActivity() {

    lateinit var controller: MobiusLoop.Controller<Model, Event>

    private var connectViewsDisposable = SerialDisposable()
    private var stopDisposables = CompositeDisposable()
    private var destroyDisposables = CompositeDisposable()

    private val update: Update<Model, Event, Effect> =
        Update { model, _ -> next(model.copy(count = model.count + 1)) }

    private val effectHandler =
        RxMobius.subtypeEffectHandler<Effect, Event>().build()

    private fun connectViews(
        models: Observable<Model>
    ): Observable<Event> {
        connectViewsDisposable.set(
            models
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { model ->
                    findViewById<TextView>(R.id.textView).text = model.count.toString()
                }
        )
        return Observable.empty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RxJavaPlugins.setErrorHandler {
            it.printStackTrace()
        }

        val stream = Observable.interval(30, TimeUnit.MILLISECONDS, Schedulers.computation())
            .map { Event }
            .publish()

        destroyDisposables.add(stream.connect())

        val loop = RxMobius.loop(update, effectHandler)
            .eventSource(RxEventSources.fromObservables(stream))

        controller = MobiusAndroid.controller(loop, Model())
    }

    override fun onStart() {
        super.onStart()
        controller.connect(RxConnectables.fromTransformer(this::connectViews))
        controller.start()

        val randomObs = Observable.fromCallable { Random.nextLong(50, 100) }
            .repeat()
            .concatMap {
                Observable.just(1)
                    .delay(it, TimeUnit.MILLISECONDS)
            }

        stopDisposables.add(
            randomObs
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (controller.isRunning) {
                        controller.stop()
                        controller.disconnect()
                    } else {
                        controller.connect(RxConnectables.fromTransformer(this::connectViews))
                        controller.start()
                    }
                }
        )
    }

    override fun onStop() {
        connectViewsDisposable.set(Disposables.disposed())
        stopDisposables.clear()
        if (controller.isRunning) {
            controller.stop()
            controller.disconnect()
        }
        super.onStop()
    }

    override fun onDestroy() {
        destroyDisposables.clear()
        super.onDestroy()
    }
}
