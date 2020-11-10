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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ActivityForm extends AppCompatActivity {
    public static String code = "";
    public static String message = "";
    CheckBox chkSetA1;
    CheckBox chkSetA2;
    CheckBox chkSetB1;
    CheckBox chkSetB2;
    String consumerPhone = "";
    EditText edtConsumerPhone;
    String hasLogin = "0";
    String hasLoginAndTransaction = "0";
    JSONObject jsonObject;
    ImageButton logoutBtn;
    Boolean network = false;
    String[] operatorList = {"017", "013", "019", "014", "016", "018", "015"};
    String setType = "";
    SharedPreferences sharedPreferences;
    Button submitBtn;
    SweetAlertDialog sweetAlertDialog;
    TextView txtName;
    TextView txtTeam;
    TextView txtTodayCount;
    TextView txtTotalCount;
    String userId = "";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_layout);
        edtConsumerPhone = (EditText) findViewById(R.id.consumerPhone);
        logoutBtn = (ImageButton) findViewById(R.id.logoutBtn);
        txtName = (TextView) findViewById(R.id.name);
        txtTeam = (TextView) findViewById(R.id.team);
        txtTodayCount = (TextView) findViewById(R.id.todayCount);
        txtTotalCount = (TextView) findViewById(R.id.totalCount);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        chkSetA1 = (CheckBox) findViewById(R.id.chkSetA1);
        chkSetA2 = (CheckBox) findViewById(R.id.chkSetA2);
        chkSetB1 = (CheckBox) findViewById(R.id.chkSetB1);
        chkSetB2 = (CheckBox) findViewById(R.id.chkSetB2);
        SharedPreferences sharedPreferences2 = getSharedPreferences("bkash_user", 0);
        sharedPreferences = sharedPreferences2;
        userId = sharedPreferences2.getString("id", (String) null);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final SweetAlertDialog log = new SweetAlertDialog(ActivityForm.this, 3);
                log.setTitleText("Are you sure to Sign Out?");
                log.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        SharedPreferences.Editor editor = ActivityForm.this.sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        log.dismissWithAnimation();
                        ActivityForm.this.finish();
                    }
                });
                log.setCancelText("No");
                log.setConfirmText("Ok");
                log.show();
            }
        });
        chkSetA1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    resetSetB();
                    setType = ExifInterface.GPS_MEASUREMENT_IN_PROGRESS;
                    hasLogin = "1";
                } else {
                    setType = "";
                    hasLogin = "0";
                }
                Log.e("check value: ", hasLogin + hasLoginAndTransaction);
            }
        });
        chkSetA2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    resetSetB();
                    setType = ExifInterface.GPS_MEASUREMENT_IN_PROGRESS;
                    hasLoginAndTransaction = "1";
                } else {
                    setType = "";
                    hasLoginAndTransaction = "0";
                }
                Log.e("check value: ", hasLogin + hasLoginAndTransaction);
            }
        });
        chkSetB1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    resetSetA();
                    setType = "B";
                    hasLogin = "1";
                } else {
                    setType = "";
                    hasLogin = "0";
                }
                Log.e("check value: ", hasLogin + hasLoginAndTransaction);
            }
        });
        chkSetB2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    resetSetA();
                    setType = "B";
                   hasLoginAndTransaction = "1";
                } else {
                    setType = "";
                   hasLoginAndTransaction = "0";
                }
                Log.e("check value: ", hasLogin + hasLoginAndTransaction);
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                network = CustomUtility.haveNetworkConnection(ActivityForm.this);
                consumerPhone = edtConsumerPhone.getText().toString();
                if (chekFeilds()) {
                    Log.e("checking", "Okkk");
                    upload();
                }
            }
        });
        if (userId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        txtName.setText(sharedPreferences.getString("name", (String) null));
        txtTeam.setText(sharedPreferences.getString("team", (String) null));
        getStatus();
    }

    public void resetSetB() {
        chkSetB1.setChecked(false);
        chkSetB2.setChecked(false);
    }

    /* access modifiers changed from: private */
    public void resetSetA() {
        chkSetA1.setChecked(false);
        chkSetA2.setChecked(false);
    }

    public boolean chekFeilds() {
        if (!network) {
            CustomUtility.showError(this, "Please turn on internet connection", "No inerternet connection!");
            return false;
        } else if (!isCorrectPhoneNumber(consumerPhone)) {
            CustomUtility.showError(this, "Please enter correct phone number", "Incorrect phone number");
            return false;
        }
        else if(hasLogin.equals("0"))
        {
            CustomUtility.showError(this,"Please select bKash login", "Required Feild!");
            return false;
        }
        else if (!setType.equals("")) {
            return true;
        } else {
            CustomUtility.showError(this, "Please select one Set", "Required Feild!");
            return false;
        }
    }

    private boolean isCorrectPhoneNumber(String phone) {
        if (phone.equals("") || (phone.length() != 11)) {
            return false;
        }
        String code2 = phone.substring(0, 3);
        for (String op : operatorList) {
            if (op.equals(code2)) {
                return true;
            }
        }
        return false;
    }

    public void getStatus() {
        sweetAlertDialog = new SweetAlertDialog(this, 5);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();
        MySingleton.getInstance(this).addToRequestQue(new StringRequest(1, "https://bkash.imslpro.com/api/consumer/user_status.php", new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    sweetAlertDialog.dismiss();
                    Log.e("response", response);
                    jsonObject = new JSONObject(response);
                    code = jsonObject.getString("success");
                    if (code.equals("true")) {
                        txtTodayCount.setText(jsonObject.getString("todayCount"));
                        txtTotalCount.setText(jsonObject.getString("totalCount"));
                        return;
                    }
                    CustomUtility.showError(ActivityForm.this, "No data found", "Failed");
                } catch (JSONException e) {
                    CustomUtility.showError(ActivityForm.this, e.getMessage(), "Getting Response");
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                sweetAlertDialog.dismiss();
                CustomUtility.showError(ActivityForm.this, "Network Error, try again!", "Failed");
                final SweetAlertDialog s = new SweetAlertDialog(ActivityForm.this, SweetAlertDialog.ERROR_TYPE);
                s.setConfirmText("Ok");
                s.setTitleText("Network Error, try again!");
                s.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        s.dismissWithAnimation();
                        startActivity(getIntent());
                        finish();
                    }
                });
                s.show();
            }
        }) {
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("UserId", userId);
                return params;
            }
        });
    }

    public void upload() {
        sweetAlertDialog = new SweetAlertDialog(this, 5);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();
        MySingleton.getInstance(this).addToRequestQue(new StringRequest(1, "https://bkash.imslpro.com/api/consumer/insert_consumer.php", new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    sweetAlertDialog.dismiss();
                    Log.e("response", response);
                    jsonObject = new JSONObject(response);
                    code = jsonObject.getString("success");
                    message = jsonObject.getString("message");
                    if (code.equals("true")) {
                        final SweetAlertDialog s = new SweetAlertDialog(ActivityForm.this, 2);
                        s.setConfirmText("Ok");
                        s.setTitleText("Successful");
                        s.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                s.dismissWithAnimation();
                                startActivity(getIntent());
                                finish();
                            }
                        });
                        s.show();
                        return;
                    }
                    Log.e("mess", ActivityForm.message);
                    CustomUtility.showError(ActivityForm.this, ActivityForm.message, "Failed");
                } catch (JSONException e) {
                    CustomUtility.showError(ActivityForm.this, e.getMessage(), "Getting Response");
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                sweetAlertDialog.dismiss();
                CustomUtility.showError(ActivityForm.this, "Network Error, try again!", "Failed");
            }
        }) {
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("ConsumerMobile", consumerPhone);
                params.put("SetType", setType);
                params.put("HasLogin", hasLogin);
                params.put("HasLoginAndTransaction", hasLoginAndTransaction);
                params.put("UserId", ActivityForm.this.userId);
                return params;
            }
        });
    }
}