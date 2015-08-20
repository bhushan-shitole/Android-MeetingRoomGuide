/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.synerzip.helloworld;

import java.util.List;

import retrofit.http.GET;

interface FiveHundredPxService {
    @GET("/v1/photos?feature=Nature&sort=rating&image_size=5&rpp=40")
    PhotosResponse getPopularPhotos();

    static class PhotosResponse {
        List<Photo> photos;
    }

    static class Photo {
        int id;
        String image_url;
        String name;
        User user;
    }

    static class User {
        String fullname;
    }
}
