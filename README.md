A contrived app to reproduce a race condition in Mobius.  Run it and after a while it should crash with a `RejectedExecutionException`:

```
01-23 23:07:40.469  9655  9678 E AndroidRuntime: io.reactivex.exceptions.UndeliverableException: The exception could not be delivered to the consumer because it has already canceled/disposed the flow or the exception has nowhere to go to begin with. Further reading: https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling | java.util.concurrent.RejectedExecutionException: Task java.util.concurrent.FutureTask@fa98f60 rejected from java.util.concurrent.ThreadPoolExecutor@9217c19[Shutting down, pool size = 1, active threads = 0, queued tasks = 0, completed tasks = 9]
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.plugins.RxJavaPlugins.onError(RxJavaPlugins.java:367)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at com.spotify.mobius.rx2.RxEventSources$1$2.accept(RxEventSources.java:69)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at com.spotify.mobius.rx2.RxEventSources$1$2.accept(RxEventSources.java:66)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.internal.observers.LambdaObserver.onError(LambdaObserver.java:77)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.internal.observers.LambdaObserver.onNext(LambdaObserver.java:67)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.internal.operators.observable.ObservablePublish$PublishObserver.onNext(ObservablePublish.java:172)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.internal.operators.observable.ObservableMap$MapObserver.onNext(ObservableMap.java:62)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.internal.operators.observable.ObservableInterval$IntervalObserver.run(ObservableInterval.java:82)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.internal.schedulers.ScheduledDirectPeriodicTask.run(ScheduledDirectPeriodicTask.java:38)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:458)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:307)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:302)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.lang.Thread.run(Thread.java:764)
01-23 23:07:40.469  9655  9678 E AndroidRuntime: Caused by: java.util.concurrent.RejectedExecutionException: Task java.util.concurrent.FutureTask@fa98f60 rejected from java.util.concurrent.ThreadPoolExecutor@9217c19[Shutting down, pool size = 1, active threads = 0, queued tasks = 0, completed tasks = 9]
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2085)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:848)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1394)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.AbstractExecutorService.submit(AbstractExecutorService.java:118)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at java.util.concurrent.Executors$DelegatedExecutorService.submit(Executors.java:634)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at com.spotify.mobius.runners.ExecutorServiceWorkRunner.post(ExecutorServiceWorkRunner.java:42)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at com.spotify.mobius.MessageDispatcher.accept(MessageDispatcher.java:50)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at com.spotify.mobius.MobiusLoop.dispatchEvent(MobiusLoop.java:141)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at com.spotify.mobius.MobiusLoop$4.accept(MobiusLoop.java:121)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at com.spotify.mobius.rx2.RxEventSources$1$1.accept(RxEventSources.java:63)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        at io.reactivex.internal.observers.LambdaObserver.onNext(LambdaObserver.java:63)
01-23 23:07:40.469  9655  9678 E AndroidRuntime:        ... 10 more
```
