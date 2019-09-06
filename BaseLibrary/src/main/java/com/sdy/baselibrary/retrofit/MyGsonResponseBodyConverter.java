/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sdy.baselibrary.retrofit;


import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.kotlin.base.rx.BaseException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static okhttp3.internal.Util.UTF_8;

final class MyGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    MyGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {

        int code = 0;
        String msg = null;
        JSONObject jsonObject;
        String response = value.string();

        try {
            jsonObject = new JSONObject(response);
            code = jsonObject.getInt("code");
            msg = jsonObject.getString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType mediaType = value.contentType();
        Charset charset = mediaType != null ? mediaType.charset(UTF_8) : UTF_8;
        InputStream inputStream = new ByteArrayInputStream(response.getBytes());
        JsonReader jsonReader = gson.newJsonReader(new InputStreamReader(inputStream, charset));

        if (code == 403) {
            value.close();
            throw new BaseException(code, msg);
        }

        try {
            return adapter.read(jsonReader);
        } finally {
            value.close();
        }
    }
}
