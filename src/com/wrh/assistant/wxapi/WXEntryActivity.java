package com.wrh.assistant.wxapi;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	private IWXAPI api;  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		api = WXAPIFactory.createWXAPI(this,  
                "wxc001385ef0e056a8", false);  
        api.handleIntent(getIntent(), this);  
	}
	
	@Override
	public void onReq(BaseReq arg0) {
	}

	@Override
	public void onResp(BaseResp arg0) {
		switch (arg0.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			Toast.makeText(this, "取消分享", Toast.LENGTH_SHORT).show();
			break;
		case BaseResp.ErrCode.ERR_SENT_FAILED:
			Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		
		this.finish();
	}
	
}
