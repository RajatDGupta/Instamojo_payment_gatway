package com.instamojo.androidsdksample.InterFace;

import com.instamojo.androidsdksample.POJO.Fetch_Token_Pojo;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by USer on 12-10-2017.
 */

public interface ServiceAPI {

    @FormUrlEncoded
    @POST("oauth2/token/")
    retrofit2.Call<Fetch_Token_Pojo> getToken(@Field("client_id") String client_id,
                                              @Field("client_secret") String client_secret,
                                              @Field("grant_type") String grant_type);
}
