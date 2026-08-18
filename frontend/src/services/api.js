import axios from "axios";


// =====================================================
// AXIOS INSTANCE
// =====================================================

const api = axios.create({

    baseURL: "http://localhost:8080/api",

    headers: {
        Accept: "application/json",
    },

});


// =====================================================
// REQUEST INTERCEPTOR
//
// Automatically attaches:
//
// Authorization: Bearer <JWT>
// =====================================================

api.interceptors.request.use(

    (config) => {

        const token =
            localStorage.getItem("token");


        console.log(
            "=============================="
        );

        console.log(
            "API REQUEST"
        );

        console.log(
            "URL:",
            config.baseURL + config.url
        );

        console.log(
            "Token exists:",
            !!token
        );


        if (token) {

            config.headers.Authorization =
                `Bearer ${token}`;

            console.log(
                "Authorization header attached"
            );

        } else {

            console.warn(
                "NO JWT TOKEN FOUND IN LOCAL STORAGE"
            );

        }


        // =================================================
        // IMPORTANT FOR FILE UPLOADS
        // =================================================
        //
        // DO NOT manually set:
        //
        // Content-Type: multipart/form-data
        //
        // Axios/browser will automatically generate:
        //
        // multipart/form-data;
        // boundary=....
        //
        // =================================================

        if (
            config.data instanceof FormData
        ) {

            delete config.headers["Content-Type"];

        }


        console.log(
            "=============================="
        );


        return config;

    },

    (error) => {

        return Promise.reject(error);

    }

);


// =====================================================
// RESPONSE INTERCEPTOR
// =====================================================

api.interceptors.response.use(

    (response) => {

        return response;

    },

    (error) => {

        console.error(
            "=============================="
        );

        console.error(
            "API ERROR"
        );

        console.error(
            "URL:",
            error.config?.url
        );

        console.error(
            "Status:",
            error.response?.status
        );

        console.error(
            "Response:",
            error.response?.data
        );

        console.error(
            "=============================="
        );


        // =================================================
        // TOKEN EXPIRED / INVALID
        // =================================================

        if (
            error.response?.status === 401
        ) {

            console.warn(
                "JWT authentication failed"
            );

            // Don't immediately delete the token.
            //
            // This makes debugging easier.
            //
            // Once everything works, we can enable:
            //
            // localStorage.removeItem("token");

        }


        return Promise.reject(error);

    }

);


export default api;