import axios from "axios";

const API = "http://localhost:8080/api/geocoding";

export const geocodeAddress = (address) => {

    const token = localStorage.getItem("token");

    return axios.post(
        API,
        {
            address
        },
        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );
};