package com.coomix.app.all.data;

import android.support.annotation.NonNull;
import com.coomix.app.all.model.response.RespBase;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

/**
 * Created by ly on 2017/9/19 10:41.
 */
public class RxUtils {
    public static void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public static FlowableTransformer schedulersTransformer() {
        return new FlowableTransformer() {
            @Override
            public Publisher apply(@io.reactivex.annotations.NonNull Flowable upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> FlowableTransformer<T, T> toMain() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> FlowableTransformer<T, T> toIO() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@io.reactivex.annotations.NonNull Flowable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
            }
        };
    }

    public static <T> FlowableTransformer<T, T> toNewThread() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@io.reactivex.annotations.NonNull Flowable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread());
            }
        };
    }

    public static <T> FlowableTransformer<T, T> toComputation() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@io.reactivex.annotations.NonNull Flowable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation());
            }
        };
    }

    public static <T> FlowableTransformer<T, T> businessTransformer() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream.map(new HandleFuc<T>()).onErrorResumeNext(new HttpResponseFunc<T>());
            }
        };
    }

    public static class HttpResponseFunc<T> implements Function<Throwable, Flowable<T>> {
        @Override
        public Flowable<T> apply(@NonNull Throwable t) throws Exception {
            return Flowable.error(ExceptionHandle.handleException(t));
        }
    }

    private static class HandleFuc<T> implements Function<T, T> {
        @Override
        public T apply(@NonNull T resp) throws Exception {
            RespBase response = (RespBase) resp;
            if (!response.isSuccess()) {
                throw new ExceptionHandle.ServerException(response.getErrcode(), response.getMsg());
            }
            return resp;
        }
    }
}
