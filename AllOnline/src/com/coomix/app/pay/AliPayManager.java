//package com.coomix.app.pay;
//
//import android.app.Activity;
//import android.content.Context;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.text.TextUtils;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.alipay.sdk.app.PayTask;
//import com.coomix.app.pay.util.OrderInfoUtil2_0;
//
//import java.util.Map;
//
//
//public class AliPayManager implements ICoomixPay
//{
//    private static final int SDK_PAY_FLAG = 1;
//    private static final int SDK_AUTH_FLAG = 2;
//    private Context mContext = null;
//
//    private Handler mHandler = new Handler(Looper.getMainLooper())
//    {
//        @SuppressWarnings("unused")
//        public void handleMessage(Message msg)
//        {
//            switch (msg.what)
//            {
//                case SDK_PAY_FLAG:
//                    AliPayResult aliPayResult = new AliPayResult((Map<String, String>) msg.obj);
//                    /**
//                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
//                     */
//                    String resultInfo = aliPayResult.getResult();// 同步返回需要验证的信息
//                    String resultStatus = aliPayResult.getResultStatus();
//                    // 判断resultStatus 为9000则代表支付成功
//                    if (TextUtils.equals(resultStatus, "9000"))
//                    {
//                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
//                        Toast.makeText(mContext, "支付成功", Toast.LENGTH_SHORT).show();
//                    }
//                    else
//                    {
//                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
//                        Toast.makeText(mContext, "支付失败", Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//
//                case SDK_AUTH_FLAG:
//                    AliAuthResult aliAuthResult = new AliAuthResult((Map<String, String>) msg.obj, true);
//                    String resultAuStatus = aliAuthResult.getResultStatus();
//
//                    // 判断resultStatus 为“9000”且result_code
//                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
//                    if (TextUtils.equals(resultAuStatus, "9000") && TextUtils.equals(aliAuthResult.getResultCode(), "200"))
//                    {
//                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
//                        // 传入，则支付账户为该授权账户
//                        Toast.makeText(mContext, "授权成功\n" + String.format("authCode:%s", aliAuthResult.getAuthCode()), Toast.LENGTH_SHORT).show();
//                    }
//                    else
//                    {
//                        // 其他状态值则为授权失败
//                        Toast.makeText(mContext, "授权失败" + String.format("authCode:%s", aliAuthResult.getAuthCode()), Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    };
//
//    public AliPayManager(Context context)
//    {
//        this.mContext = context;
//    }
//
//    @Override
//    public void registerApp(String appId)
//    {
//
//    }
//
//    /**
//     * 支付宝支付业务
//     *
//     */
//    @Override
//    public void pay(CoomixPayRsp coomixPayRsp)
//    {
////        if(coomixPayRsp == null)
////        {
////            return;
////        }
////
////        String RSA_PRIVATE = "";
////        String APPID = "";
////        String RSA2_PRIVATE = "";
////        if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE)))
////        {
////            Toast.makeText(mContext, "配置信息为空，无法发起支付！", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        /**
////         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
////         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
////         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
////         *
////         * orderInfo的获取必须来自服务端；
////         */
////        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
////        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2);
////        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
////        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
////        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
////        final String orderInfo = orderParam + "&" + sign;
////
////        Runnable payRunnable = new Runnable()
////        {
////            @Override
////            public void run()
////            {
////                Activity activity = null;
////                if(mContext instanceof  Activity)
////                {
////                    activity = (Activity) mContext;
////                }
////                else
////                {
////                    activity = MainActivity.mActivity;
////                }
////
////                //必须在子线程发起
////                PayTask alipay = new PayTask(activity);
////                Map<String, String> result = alipay.payV2(orderInfo, true);
////
////                if(Constant.IS_DEBUG_MODE)
////                {
////                    Log.i("Test", result.toString());
////                }
////
////                Message msg = mHandler.obtainMessage();
////                msg.what = SDK_PAY_FLAG;
////                msg.obj = result;
////                mHandler.sendMessage(msg);
////            }
////        };
////
////        Thread payThread = new Thread(payRunnable);
////        payThread.start();
//    }
//
//    @Override
//    public void release()
//    {
//
//    }
//}
