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
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
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

    private var streamDisposable = SerialDisposable()
    private var toggleDisposable = SerialDisposable()

    private val update: Update<Model, Event, Effect> =
        Update { model, _ -> next(model.copy(count = model.count + 1)) }

    private val effectHandler =
        RxMobius.subtypeEffectHandler<Effect, Event>().build()

    private fun connectViews(
        models: Observable<Model>
    ): Observable<Event> {

        val disposable = models
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { model ->
                    findViewById<TextView>(R.id.textView).text = model.count.toString()
                }

        return Observable.empty<Event>()
            .doOnDispose(disposable::dispose)

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

        streamDisposable.set(stream.connect())

        val loop = RxMobius.loop(update, effectHandler)
            .eventSource(RxEventSources.fromObservables(stream))

        controller = MobiusAndroid.controller(loop, Model())
    }

    override fun onStart() {
        super.onStart()

        controller.connect(RxConnectables.fromTransformer(this::connectViews))
        controller.start()

        val randomObs = Flowable.fromCallable { Random.nextLong(50, 100) }
            .repeat()
            .concatMap {
                Flowable.just(1)
                    .delay(it, TimeUnit.MILLISECONDS)
            }

        toggleDisposable.set(
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
        toggleDisposable.dispose()
        streamDisposable.dispose()

        if (controller.isRunning) {
            controller.stop()
            controller.disconnect()
        }
        super.onStop()
    }
}
