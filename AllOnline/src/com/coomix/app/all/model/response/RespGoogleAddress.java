package com.coomix.app.all.model.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespGoogleAddress implements Serializable {
    private static final long serialVersionUID = 1L;

//    JSONObject json = new JSONObject(content);
//    JSONArray jarray = json.optJSONArray("results");
//                    if (jarray != null && jarray.length() > 0) {
//        result.statusCode = Result.OK;
//        JSONObject jobj = jarray.optJSONObject(0);
//        if (jobj != null) {
//            result.mResult = jobj.optString("formatted_address");
//        }
//    }
    private ArrayList<GoogleAddress> results;

    public ArrayList<GoogleAddress> getResults() {
        return results;
    }

    public void setData(ArrayList<GoogleAddress> data) {
        this.results = data;
    }

    public class GoogleAddress implements Serializable
    {
        public String formatted_address;
    }
}
