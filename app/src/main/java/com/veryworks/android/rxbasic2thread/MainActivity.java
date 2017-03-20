package com.veryworks.android.rxbasic2thread;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/* Rx에서 Thread 지정
- Schedulers.computation() - 이벤트 그룹에서 간단한 연산이나 콜백 처리를 위해 사용. RxComputationThreadPool라는 별도의 스레드 풀에서 최대 cpu갯수 만큼 순환하면서 실행
- Schedulers.immediate() - 현재 스레드에서 즉시 수행. observeOn()이 여러번 쓰였을 경우 immediate()를 선언한 바로 윗쪽의 스레드에서 실행.
- Schedulers.from(executor) - 특정 executor를 스케쥴러로 사용.

- Schedulers.io() - 동기 I/O를 별도로 처리시켜 비동기 효율을 얻기 위한 스케줄러. 자체적인 스레드 풀 CachedThreadPool을 사용. API 호출 등 네트워크를 사용한 호출 시 사용.
- Schedulers.newThread() - 새로운 스레드를 만드는 스케쥴러
- Schedulers.trampoline() - 큐에 있는 일이 끝나면 이어서 현재 스레드에서 수행하는 스케쥴러.

- AndroidSchedulers.mainThread() - 안드로이드의 UI 스레드에서 동작
- HandlerScheduler.from(handler) - 특정 핸들러 handler에 의존하여 동작

* 사용 시 주의사항
- Schedulers.computation() - 이벤트 룹에서 간단한 연산이나 콜백 처리를 위해서 사용. I/O 처리를 여기에서 해서는 안됨.
- Schedulers.io() - 동기 I/O를 별도로 처리시켜 비동기 효율을 얻기 위한 스케줄러. 자체적인 스레드 풀에 의존.
  일부 오퍼레이터들은 자체적으로 어떤 스케쥴러를 사용할지 지정한다. 예를 들어 buffer 오퍼레이터는 Schedulers.computation()에 의존하며 repeat은 Schedulers.trampoline()를 사용한다.

- RxAndroid 지정 Scheduler
  AndroidSchedulers.mainThread() - 안드로이드의 UI 스레드에서 동작.
  HandlerScheduler.from(handler) - 특정 핸들러 handler에 의존하여 동작.

- RxAndroid가 제공하는 AndroidSchedulers.mainThread() 와
  RxJava가 제공하는 Schedulers.io()를 조합해서 Schedulers.io()에서 수행한 결과를 AndroidSchedulers.mainThread()에서 받아 UI에 반영하는 형태가 많이 사용되고 있다
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    TextView text1,text2,text3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);
        text3 = (TextView) findViewById(R.id.text3);

        // 실제 Task 처리하는 객체 ( 발행자 )
        Observable<String> simpleObservable =
            Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    // 네트웍을 통해서 데이터를 긁어온다
                    // 반복문을 돌면서 -----------------------
                    for(int i=0 ; i<10 ; i++) {
                        subscriber.onNext("Hello RxAndroid "+i); // onNext 로 구독자들에게 데이터를 보내준다
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // --------------------------------------
                    subscriber.onCompleted();
                }
            });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 기본형 - thread 를 지정해준다
        simpleObservable
                .subscribeOn(Schedulers.io()) // 발행자를 별도의 thread 에서 동작 시킨다
                .observeOn(AndroidSchedulers.mainThread()) // 구독자를 mainThread 에서 동작시킨다
            .subscribe(new Subscriber<String>() { // Observer (구독자)
                @Override
                public void onCompleted() {
                    Log.d(TAG, "[Observer1] complete");
                }
                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "[Observer1] error: " + e.getMessage());
                }
                @Override
                public void onNext(String text) {
                    text1.setText("[Observer1] "+text);
                }
            });


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 옵저버를 등록하는 함수 - 진화형 (각 함수를 하나의 콜백객체에 나눠서 담아준다)
        simpleObservable
                .subscribeOn(Schedulers.io()) // 발행자를 별도의 thread 에서 동작 시킨다
                .observeOn(AndroidSchedulers.mainThread()) // 구독자를 mainThread 에서 동작시킨다
                .subscribe(new Action1<String>() { // onNext 함수와 동일한 역할을 하는 콜백객체
            @Override
            public void call(String s) {
                text2.setText("[Observer2] "+s);
            }
        }, new Action1<Throwable>() { // onError 함수와 동일한 역할을 하는 콜백 객체
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG, "[Observer2] error: " + throwable.getMessage());
            }
        }, new Action0() { // onComplete 과 동일한 역할을 하는 콜백 객체
            @Override
            public void call() {
                Log.d(TAG, "[Observer2] complete");
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 옵저버를 등록하는 함수 - 최종진화형(람다식)
        simpleObservable
                .subscribeOn(Schedulers.io()) // 발행자를 별도의 thread 에서 동작 시킨다
                .observeOn(AndroidSchedulers.mainThread()) // 구독자를 mainThread 에서 동작시킨다
                .subscribe(
            (string) -> {text3.setText("[Observer3] "+string);}
            ,(error) -> {Log.e(TAG, "[Observer3] error: " + error.getMessage());}
            ,() -> {Log.d(TAG, "[Observer3] complete");}
        );
    }
}
