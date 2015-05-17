package com.wrh.assistant.utils;

import java.io.ByteArrayOutputStream;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class WeiXinUtil {
	
	public static final String PACKAGENAME = "com.tencent.mm";
	private static final int THUMB_SIZE = 150;
	
	private Context context;
	private String appId ;
	private IWXAPI api;
	private boolean isRegisterSuccess;
	
	public WeiXinUtil(Context context,String APP_ID) {
		this.context = context;
		this.appId = APP_ID;
	
		api = WXAPIFactory.createWXAPI(context, APP_ID);
		isRegisterSuccess = api.registerApp(APP_ID);
	}
	
	/***
	 * 
	 * @return 返回注册成功与否
	 */
	public boolean isRegisterSuccess() {
		return isRegisterSuccess;
	}
	
	public IWXAPI getApi() {
		return api;
	}

	/**
	 * 分享文本消息到朋友圈
	 * @param shareText
	 * @return
	 */
	public boolean sendTextReqToNetWorking(String shareText){
		WXTextObject textObject = new WXTextObject();
		textObject.text = shareText;
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObject;;
		msg.description = shareText;
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline; // 发送的目标场景是朋友圈
		
		return api.sendReq(req);
	}
	/**
	 * 分享文本消息给好友
	 * @param shareText
	 * @return
	 */
	public boolean sendTextReqToFriends(String shareText){
		WXTextObject textObject = new WXTextObject();
		textObject.text = shareText;
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObject;;
		msg.description = shareText;
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession; // 发送的目标场景是会话
		
		return api.sendReq(req);
	}
	
	/**
	 * 分享图片文本消息到朋友圈
	 * @param 
	 * @return
	 */
	public boolean sendImageReqToNetWoring(String title,String desc,Bitmap shareBitmap){
		
		WXImageObject imageObject = new WXImageObject(shareBitmap);
		
		WXMediaMessage mediaMessage = new WXMediaMessage();
		mediaMessage.title = title;
		mediaMessage.description = desc;
		mediaMessage.mediaObject = imageObject;
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(shareBitmap, THUMB_SIZE, THUMB_SIZE, true);
//		shareBitmap.recycle();
		mediaMessage.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = mediaMessage;
		req.scene = SendMessageToWX.Req.WXSceneTimeline; // 发送的目标场景是朋友圈
		
		return api.sendReq(req);
	}
	
	/**
	 * 分享图片文本消息给好友
	 * @param
	 * @return
	 */
	public boolean sendImageReqToFriends(String title,String desc,Bitmap shareBitmap){
		
		WXImageObject imageObject = new WXImageObject(shareBitmap);
		
		WXMediaMessage mediaMessage = new WXMediaMessage();
		mediaMessage.title = title;
		mediaMessage.description = desc;
		mediaMessage.mediaObject = imageObject;
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(shareBitmap, THUMB_SIZE, THUMB_SIZE, true);
//		shareBitmap.recycle();
		mediaMessage.thumbData = bmpToByteArray(thumbBmp, true);  // 设置缩略图

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = mediaMessage;
		req.scene = SendMessageToWX.Req.WXSceneSession; // 发送的目标场景是会话
		
		return api.sendReq(req);
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
