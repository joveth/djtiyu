package com.djtiyu.m.djtiyu;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.sina.weibo.sdk.api.share.BaseResponse;
import com.umeng.socialize.media.WBShareCallBackActivity;

/**
 * Created by wangfei on 15/12/3.
 */
public class WBShareActivity extends WBShareCallBackActivity{


  @Override
  public void onResponse(BaseResponse baseResponse) {
    if(baseResponse.errCode==1){

    }else{

    }
  }
}
