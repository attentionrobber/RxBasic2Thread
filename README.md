# Rx Thread
Rx 에서 Thread를 지정하는 방법을 알아봅니다


## Rx에서 Thread 지정
- Schedulers.computation() - 이벤트 그룹에서 간단한 연산이나 콜백 처리를 위해 사용. RxComputationThreadPool라는 별도의 스레드 풀에서 최대 cpu갯수 만큼 순환하면서 실행
- Schedulers.immediate() - 현재 스레드에서 즉시 수행. observeOn()이 여러번 쓰였을 경우 immediate()를 선언한 바로 윗쪽의 스레드에서 실행.
- Schedulers.from(executor) - 특정 executor를 스케쥴러로 사용.

- Schedulers.io() - 동기 I/O를 별도로 처리시켜 비동기 효율을 얻기 위한 스케줄러. 자체적인 스레드 풀 CachedThreadPool을 사용. API 호출 등 네트워크를 사용한 호출 시 사용.
- Schedulers.newThread() - 새로운 스레드를 만드는 스케쥴러
- Schedulers.trampoline() - 큐에 있는 일이 끝나면 이어서 현재 스레드에서 수행하는 스케쥴러.

- AndroidSchedulers.mainThread() - 안드로이드의 UI 스레드에서 동작
- HandlerScheduler.from(handler) - 특정 핸들러 handler에 의존하여 동작

## 사용 시 주의사항
- Schedulers.computation() - 이벤트 룹에서 간단한 연산이나 콜백 처리를 위해서 사용. I/O 처리를 여기에서 해서는 안됨.
- Schedulers.io() - 동기 I/O를 별도로 처리시켜 비동기 효율을 얻기 위한 스케줄러. 자체적인 스레드 풀에 의존.
  일부 오퍼레이터들은 자체적으로 어떤 스케쥴러를 사용할지 지정한다. 예를 들어 buffer 오퍼레이터는 Schedulers.computation()에 의존하며 repeat은 Schedulers.trampoline()를 사용한다.

- RxAndroid 지정 Scheduler
  AndroidSchedulers.mainThread() - 안드로이드의 UI 스레드에서 동작.
  HandlerScheduler.from(handler) - 특정 핸들러 handler에 의존하여 동작.

- RxAndroid가 제공하는 AndroidSchedulers.mainThread() 와
  RxJava가 제공하는 Schedulers.io()를 조합해서 Schedulers.io()에서 수행한 결과를 AndroidSchedulers.mainThread()에서 받아 UI에 반영하는 형태가 많이 사용되고 있다
