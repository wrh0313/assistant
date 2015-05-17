package com.wrh.assistant.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class QQUtil {
	private Context context;
	private String appId;
	private Tencent tencent;
	
	public QQUtil(Context context,String APP_ID) {
		this.context = context;
		this.appId = APP_ID;
		tencent = Tencent.createInstance(appId, context.getApplicationContext());
	}
	
	
	/****
	 * 分享图文消息到QQ空间
	 * @param shareTitle   分享标题（必填）
	 * @param shareSummary 分享摘要（可填）
	 * @param targetUrl	         这条消息被好友点击后跳转的URL（必填）      
	 * @param imageUrls    图片url(本地路径或图片Url)，支持多张图片（可填）
	 */
	public void shareTextImageToQQZ(String shareTitle,String shareSummary,String targetUrl,ArrayList<String>imageUrls){
		final Bundle params = new Bundle();
		
		params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
		params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareTitle);
		params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareSummary);
		params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
		params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
		
		doShareToQzone(params);
	}
	
	/*****
	 * 分享纯图消息到QQ空间
	 * @param imageLocalUrl 需要分享的本地图片的路径
	 */
	public void shareImageToQQZ(String imageLocalUrl){
		final Bundle params = new Bundle();
		
		params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE);
		params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageLocalUrl);
		
		doShareToQzone(params);
	} 
	
	/**
     * 用异步方式启动分享
     * @param params
     */
    private void doShareToQzone(final Bundle params) {
    	tencent.shareToQzone((Activity)context,params ,new IUiListener() {
			@Override
			public void onError(UiError arg0) {
				// TODO Auto-generated method stub
            	Log.i("abc", "onError:"+arg0.errorCode+","+arg0.errorMessage);
				Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onComplete(Object arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "分享成功", Toast.LENGTH_SHORT).show();

			}
			
			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				Toast.makeText(context, "取消分享", Toast.LENGTH_SHORT).show();
				
			}
		} );
    }
}
