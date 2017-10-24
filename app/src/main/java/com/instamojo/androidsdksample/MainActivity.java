package com.example.user.salaryin.payment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.user.salaryin.BuildConfig;
import com.example.user.salaryin.Network.ApiClient;
import com.example.user.salaryin.POJO.Fetch_Token_Pojo;
import com.example.user.salaryin.R;
import com.example.user.salaryin.Service.ServiceAPI;
import com.example.user.salaryin.Session.TrypuneSession;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.instamojo.android.Instamojo;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.callbacks.OrderRequestCallBack;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.models.Errors;
import com.instamojo.android.models.Order;
import com.instamojo.android.network.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InstaMojoActivity extends AppCompatActivity {

 /*for getting token these three constant requird
    * documentation https://docs.instamojo.com/page/android-sdk*
    *
    * to get tokent url=https://test.instamojo.com/oauth2/token/************/

    public static final String client_id = "d1EKYiJzoiAAEUmG4cipMNqbQryFEKeKyovzph6i";
    public static final String client_secret = "DIt2iUDSShQqozV1rHl7SWqMxcXkQVM2FKVRzLMIaFydErfQ2Vif3JqPj3OtWT36foNqZt2yX74TMmsGWMLIfnmwnR1JpSGUMP4FO7NUsGgYlbN9rYapfykvAjB8TUSA";
    public static final String grant_type = "client_credentials";
    
    
    private static class DefaultHeadersInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            return chain.proceed(chain.request()
                    .newBuilder()
                    .header("User-Agent", getUserAgent())
                    .header("Referer", getReferer())
                    .build());
        }

        private static String getUserAgent() {
            return "instamojo-android-sdk-sample/" + BuildConfig.VERSION_NAME
                    + " android/" + Build.VERSION.RELEASE
                    + " " + Build.BRAND + "/" + Build.MODEL;
        }

        private static String getReferer() {
            return "android-app://" + BuildConfig.APPLICATION_ID;
        }
    }


    public String client_id = null;
    public String client_secret = null;
    public String grant_type = null;
    TrypuneSession trypuneSession;
    public String user_Name, user_Id, user_Email, user_Amount, user_Desc, user_Mobile, address, pincode, add_pin, date;
    private ProgressDialog dialog;
    private AppCompatEditText nameBox, emailBox, phoneBox, amountBox, descriptionBox, addressBox, pincodeBox;
    Button button;
    private String currentEnv = null;
    private String accessToken = null;


    private static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new DefaultHeadersInterceptor())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instamojo);

        getConstants();
        valueFromSession();
        initView();
        toolBar();


        currentEnv = "https://api.instamojo.com/";
        Instamojo.setBaseUrl(currentEnv);


        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    fetchAccessToken();
                }
            }
        });


        //let's set the log level to debug
        Instamojo.setLogLevel(Log.DEBUG);
    }

    private void toolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Payment Details");

    }

    private void initView() {
        button = (Button) findViewById(R.id.pay);

        nameBox = (AppCompatEditText) findViewById(R.id.name);
        nameBox.setText(user_Name);

        emailBox = (AppCompatEditText) findViewById(R.id.email);
        emailBox.setText(user_Email);

        phoneBox = (AppCompatEditText) findViewById(R.id.phone);
        phoneBox.setText(user_Mobile);

        addressBox = (AppCompatEditText) findViewById(R.id.address);

        pincodeBox = (AppCompatEditText) findViewById(R.id.pincode);

        amountBox = (AppCompatEditText) findViewById(R.id.amount);
        amountBox.setText(user_Amount);

        descriptionBox = (AppCompatEditText) findViewById(R.id.description);
        descriptionBox.setText(user_Desc);
    }

    private void valueFromSession() {
        trypuneSession = new TrypuneSession(getApplicationContext());

        HashMap<String, String> user = trypuneSession.getUserDetails();

        user_Name = user.get(TrypuneSession.KEY_NAME);
        user_Id = user.get(TrypuneSession.KEY_ID);
        user_Email = user.get(TrypuneSession.KEY_EMAIL);
        user_Amount = user.get(TrypuneSession.KEY_AMOUNT);
        user_Desc = user.get(TrypuneSession.KEY_DESC);
        user_Mobile = user.get(TrypuneSession.KEY_MOBILE);
    }

    private void getConstants() {
        client_id = PaymentConstants.client_id;
        client_secret = PaymentConstants.client_secret;
        grant_type = PaymentConstants.grant_type;
    }

    public String repayDate() {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
        return Integer.toString(mDay) + "/" + Integer.toString(mMonth + 1) + "/" + Integer.toString(mYear);

    }

    /*************************validation*********************************/
    public boolean validate() {
        String validate_address = addressBox.getText().toString().trim();
        String validate_pincode = pincodeBox.getText().toString().trim();

        if (validate_address.isEmpty()) {
            addressBox.setFocusable(true);
            addressBox.setError("please enter valid address");
            return false;
        }
        if (validate_pincode.length() < 6) {
            pincodeBox.setFocusable(true);
            pincodeBox.setError("please enter valid pincode");
            return false;
        }
        return true;
    }


    /**********************************instamojo create order***************************************/
    private void createOrder(String accessToken, String transactionID) {

        String name = nameBox.getText().toString();
        final String email = emailBox.getText().toString();
        String phone = phoneBox.getText().toString();
        String amount = amountBox.getText().toString();
        String description = descriptionBox.getText().toString();

        //Create the Order
        Order order = new Order(accessToken, transactionID, name, email, phone, amount, description);

        //set webhook
        // order.setWebhook("http://your.server.com/webhook/");

        //Validate the Order
        if (!order.isValid()) {
            //oops order validation failed. Pinpoint the issue(s).

            if (!order.isValidName()) {
                nameBox.setError("Buyer name is invalid");
            }

            if (!order.isValidEmail()) {
                emailBox.setError("Buyer email is invalid");
            }

            if (!order.isValidPhone()) {
                phoneBox.setError("Buyer phone is invalid");
            }

            if (!order.isValidAmount()) {
                amountBox.setError("Amount is invalid or has more than two decimal places");
            }

            if (!order.isValidDescription()) {
                descriptionBox.setError("Description is invalid");
            }

            if (!order.isValidTransactionID()) {
                showToast("Transaction is Invalid");
            }

            if (!order.isValidRedirectURL()) {
                showToast("Redirection URL is invalid");
            }

            if (!order.isValidWebhook()) {
                showToast("Webhook URL is invalid");
            }

            return;
        }

        //Validation is successful. Proceed
        dialog.show();
        Request request = new Request(order, new OrderRequestCallBack() {
            @Override
            public void onFinish(final Order order, final Exception error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (error != null) {
                            if (error instanceof Errors.ConnectionError) {
                                showToast("No internet connection");
                            } else if (error instanceof Errors.ServerError) {
                                showToast("Server error. Try again");
                            } else if (error instanceof Errors.AuthenticationError) {
                                showToast("Access token is invalid or expired. Please Update the token.");
                            } else if (error instanceof Errors.ValidationError) {
                                // Cast object to validation to pinpoint the issue
                                Errors.ValidationError validationError = (Errors.ValidationError) error;

                                if (!validationError.isValidTransactionID()) {
                                    showToast("Transaction ID is not Unique");
                                    return;
                                }

                                if (!validationError.isValidRedirectURL()) {
                                    showToast("Redirect url is invalid");
                                    return;
                                }

                                if (!validationError.isValidWebhook()) {
                                    showToast("Webhook url is invalid");
                                    return;
                                }

                                if (!validationError.isValidPhone()) {
                                    phoneBox.setError("Buyer's Phone Number is invalid/empty");
                                    return;
                                }

                                if (!validationError.isValidEmail()) {
                                    emailBox.setError("Buyer's Email is invalid/empty");
                                    return;
                                }

                                if (!validationError.isValidAmount()) {
                                    amountBox.setError("Amount is either less than Rs.9 or has more than two decimal places");
                                    return;
                                }

                                if (!validationError.isValidName()) {
                                    nameBox.setError("Buyer's Name is required");
                                    return;
                                }
                            } else {
                                showToast(error.getMessage());
                            }
                            return;
                        }

                        startPreCreatedUI(order);
                    }
                });
            }
        });

        request.execute();
    }


    /*************instamojo create UI********************/
    private void startPreCreatedUI(Order order) {
        //Using Pre created UI
        Intent intent = new Intent(getBaseContext(), PaymentDetailsActivity.class);
        intent.putExtra(Constants.ORDER, order);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }


    /*************instamojo custom toast********************/
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /*************instamojo create Token********************/
    private void fetchAccessToken() {

        if (!dialog.isShowing()) {
            dialog.show();
        }

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.instamojo.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        ServiceAPI serviceAPI = retrofit.create(ServiceAPI.class);
        retrofit2.Call<Fetch_Token_Pojo> fetch_token_pojoCall = serviceAPI.getToken(client_id, client_secret, grant_type);
        fetch_token_pojoCall.enqueue(new retrofit2.Callback<Fetch_Token_Pojo>() {
            @Override
            public void onResponse(retrofit2.Call<Fetch_Token_Pojo> call, retrofit2.Response<Fetch_Token_Pojo> response) {
                Fetch_Token_Pojo fetch_token_pojo = response.body();

                String errorMessage = null;
                String transactionID = null;

                try {

                    if (fetch_token_pojo != null) {
                        accessToken = fetch_token_pojo.getAccessToken();
                        transactionID = fetch_token_pojo.getAccessToken();
                    }

                } catch (Exception e) {
                    errorMessage = "Failed to fetch order tokens";
                }

                final String finalErrorMessage = errorMessage;
                final String finalTransactionID = transactionID;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        if (finalErrorMessage != null) {
                            showToast(finalErrorMessage);
                            return;
                        }

                        createOrder(accessToken, finalTransactionID);
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<Fetch_Token_Pojo> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        showToast("Failed to fetch the Order Tokens");
                    }
                });
            }
        });

    }

    /**
     * Will check for the transaction status of a particular Transaction
     *
     * @param transactionID Unique identifier of a transaction ID
     */
    private void checkPaymentStatus(final String transactionID, final String orderID) {
        if (accessToken == null || (transactionID == null && orderID == null)) {
            return;
        }

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }

        showToast("Checking transaction status");
        HttpUrl.Builder builder = getHttpURLBuilder();
        builder.addPathSegment("status");
        if (transactionID != null) {
            builder.addQueryParameter("transaction_id", transactionID);
        } else {
            builder.addQueryParameter("id", orderID);
        }
        builder.addQueryParameter("env", currentEnv.toLowerCase());
        HttpUrl url = builder.build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        showToast("Failed to fetch the transaction status");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                response.body().close();
                String status = null;
                String paymentID = null;
                String amount = null;
                String errorMessage = null;

                try {
                    JSONObject responseObject = new JSONObject(responseString);
                    JSONObject payment = responseObject.getJSONArray("payments").getJSONObject(0);
                    status = payment.getString("status");
                    paymentID = payment.getString("id");
                    amount = responseObject.getString("amount");

                } catch (JSONException e) {
                    errorMessage = "Failed to fetch the transaction status";
                }

                final String finalStatus = status;
                final String finalErrorMessage = errorMessage;
                final String finalPaymentID = paymentID;
                final String finalAmount = amount;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (finalStatus == null) {
                            showToast(finalErrorMessage);
                            return;
                        }

                        if (!finalStatus.equalsIgnoreCase("successful")) {
                            showToast("Transaction still pending");
                            return;
                        } else {

                            address = addressBox.getText().toString().trim();
                            pincode = pincodeBox.getText().toString().trim();
                            add_pin = address + " " + pincode;
                            date = repayDate();

                            ServiceAPI serviceAPI = ApiClient.getRetrofit().create(ServiceAPI.class);
                            retrofit2.Call<Fetch_Token_Pojo> pojoCall = serviceAPI.storeStatus(user_Id, finalPaymentID, date, finalAmount, finalStatus, add_pin);
                            pojoCall.enqueue(new retrofit2.Callback<Fetch_Token_Pojo>() {
                                @Override
                                public void onResponse(retrofit2.Call<Fetch_Token_Pojo> call, retrofit2.Response<Fetch_Token_Pojo> response) {

                                    showToast("Transaction successful for id - " + finalPaymentID);
                                }

                                @Override
                                public void onFailure(retrofit2.Call<Fetch_Token_Pojo> call, Throwable t) {

                                }
                            });
                        }

                        showToast("Transaction successful for id - " + finalStatus);

                    }
                });
            }
        });

    }


    private HttpUrl.Builder getHttpURLBuilder() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("sample-sdk-server.instamojo.com");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && data != null) {
            String orderID = data.getStringExtra(Constants.ORDER_ID);
            String transactionID = data.getStringExtra(Constants.TRANSACTION_ID);
            String paymentID = data.getStringExtra(Constants.PAYMENT_ID);

            // Check transactionID, orderID, and orderID for null before using them to check the Payment status.
            if (transactionID != null || paymentID != null) {
                checkPaymentStatus(transactionID, orderID);
            } else {
                showToast("Oops!! Payment was cancelled");
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
