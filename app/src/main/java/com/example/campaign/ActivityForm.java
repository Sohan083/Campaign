package com.example.campaign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivityForm extends AppCompatActivity {

    EditText edtConsumerPhone;
    ImageButton logoutBtn;
    Button submitBtn;
    CheckBox chkSetA1, chkSetA2, chkSetB1, chkSetB2;
    String consumerPhone = "";
    Boolean network = false;

    String[] operatorList = new String[]{"017","013","019","014","016","018","015"};
    SharedPreferences sharedPreferences;

    String setType = "", hasLogin = "0", hasLoginAndTransaction = "0", userId = "";

    SweetAlertDialog sweetAlertDialog;
    JSONObject jsonObject;
    public static String code = "", message = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_layout);

        edtConsumerPhone = findViewById(R.id.consumerPhone);
        logoutBtn = findViewById(R.id.logoutBtn);
        submitBtn = findViewById(R.id.submitBtn);
        chkSetA1 = findViewById(R.id.chkSetA1);
        chkSetA2 = findViewById(R.id.chkSetA2);
        chkSetB1 = findViewById(R.id.chkSetB1);
        chkSetB2 = findViewById(R.id.chkSetB2);

        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        userId = sharedPreferences.getString("id",null);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SweetAlertDialog log = new SweetAlertDialog(ActivityForm.this, SweetAlertDialog.WARNING_TYPE);
                log.setTitleText("Are you sure to Sign Out?");
                log.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        log.dismissWithAnimation();
                        finish();
                    }
                });
                log.setCancelText("No");
                log.setConfirmText("Ok");
                log.show();
            }
        });
        chkSetA1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    resetSetB();
                    setType = "A";
                    hasLogin = "1";
                }
                else
                {
                    setType = "";
                    hasLogin = "0";
                }
                Log.e("check value: ",hasLogin+hasLoginAndTransaction);
            }
        });
        chkSetA2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    resetSetB();
                    setType = "A";
                    hasLoginAndTransaction = "1";
                }
                else
                {
                    setType = "";
                    hasLoginAndTransaction = "0";
                }
                Log.e("check value: ",hasLogin+hasLoginAndTransaction);
            }
        });
        chkSetB1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    resetSetA();
                    setType = "B";
                    hasLogin = "1";
                }
                else
                {
                    setType = "";
                    hasLogin = "0";
                }
                Log.e("check value: ",hasLogin+hasLoginAndTransaction);
            }
        });
        chkSetB2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    resetSetA();
                    setType = "B";
                    hasLoginAndTransaction = "1";
                }
                else
                {
                    setType = "";
                    hasLoginAndTransaction = "0";
                }
                Log.e("check value: ",hasLogin+hasLoginAndTransaction);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                network = CustomUtility.haveNetworkConnection(ActivityForm.this);
                consumerPhone = edtConsumerPhone.getText().toString();
                boolean flag = chekFeilds();
                if(flag)
                {
                    Log.e("checking","Okkk");
                    upload();
                }
            }
        });

    }

    private void resetSetB()
    {
        chkSetB1.setChecked(false);
        chkSetB2.setChecked(false);
    }
    private void resetSetA()
    {
        chkSetA1.setChecked(false);
        chkSetA2.setChecked(false);
    }

    private boolean chekFeilds()
    {
        if (!network)
        {
            CustomUtility.showError(ActivityForm.this,"Please turn on internet connection","No inerternet connection!");
            return false;
        }
        else if(!isCorrectPhoneNumber(consumerPhone))
        {
            CustomUtility.showError(ActivityForm.this,"Please enter correct phone number","Incorrect phone number");
            return false;
        }
        else if(setType.equals(""))
        {
            CustomUtility.showError(ActivityForm.this,"Please select one Set","Required Feild!");
            return false;
        }
        return true;
    }

    private boolean isCorrectPhoneNumber(String phone)
    {
        boolean is = false;
        if(phone.equals("") | phone.length()!=11) return is;
        String code = phone.substring(0,3);
        for (String op:operatorList) {
            if(op.equals(code))
            {
                is = true;
                break;
            }
        }
        return is;
    }

    public void upload()
    {
        sweetAlertDialog = new SweetAlertDialog(ActivityForm.this,SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();
        String upLoadServerUri = "https://bkash.imslpro.com/api/consumer/insert_consumer.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, upLoadServerUri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            sweetAlertDialog.dismiss();
                            Log.e("response",response);
                            jsonObject = new JSONObject(response);
                            code = jsonObject.getString("success");
                            message = jsonObject.getString("message");
                            if (code.equals("true")) {
                                final SweetAlertDialog s = new SweetAlertDialog(ActivityForm.this,SweetAlertDialog.SUCCESS_TYPE);
                                s.setConfirmText("Ok");
                                s.setTitleText("Successful");
                                s.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        s.dismissWithAnimation();
                                        startActivity(getIntent());
                                        finish();
                                    }
                                });
                                s.show();
                            }
                            else{
                                Log.e("mess",message);
                                CustomUtility.showError(ActivityForm.this,message,"Failed");
                                return;
                            }
                        } catch (JSONException e) {
                            CustomUtility.showError(ActivityForm.this, e.getMessage(), "Getting Response");

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sweetAlertDialog.dismiss();
                CustomUtility.showError(ActivityForm.this, "Network Error, try again!", "Failed");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("ConsumerMobile",consumerPhone);
                params.put("SetType",setType);
                params.put("HasLogin",hasLogin);
                params.put("HasLoginAndTransaction",hasLoginAndTransaction);
                params.put("UserId",userId);
                return params;
            }
        };

        MySingleton.getInstance(ActivityForm.this).addToRequestQue(stringRequest);
    }
}
