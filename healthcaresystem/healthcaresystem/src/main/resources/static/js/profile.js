document.addEventListener("DOMContentLoaded", async function () {
    const token = localStorage.getItem("authToken"); // Assuming token is stored in localStorage

    if (!token) {
        alert("Unauthorized! Please log in.");
        window.location.href = "/login.html"; // Redirect to login page if not logged in
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/user/getProfile", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error("Failed to fetch profile data.");
        }

        const data = await response.json();

        if (data.status === "success") {
            updateProfileUI(data.profile);
        } else {
            alert("Error: " + data.message);
        }
    } catch (error) {
        console.error("Error fetching profile data:", error);
        alert("Could not load profile. Try again later.");
    }
});

function updateProfileUI(profile) {
    localStorage.setItem("name", profile.username);
    document.querySelector(".profile-header h2").innerText = profile.username || "User";
    document.querySelector(".profile-header p").innerHTML = `Health Status: <strong>${profile.activity_status || "Unknown"}</strong>`;

    document.querySelector(".profile-details").innerHTML = `
        <h3>Health Information</h3>
        <p><strong>Age:</strong> ${profile.age || "N/A"}</p>
        <p><strong>Gender:</strong> ${profile.gender || "N/A"}</p>
        <p><strong>Blood Group:</strong> ${profile.blood_group || "N/A"}</p>
        <p><strong>Height:</strong> ${profile.height_cm ? profile.height_cm + " cm" : "N/A"}</p>
        <p><strong>Weight:</strong> ${profile.weight_kg ? profile.weight_kg + " kg" : "N/A"}</p>
        <p><strong>BMI:</strong> ${profile.bmi ? profile.bmi.toFixed(1) : "N/A"}</p>
        <p><strong>Exercise Plan:</strong> ${profile.exercise_plan || "N/A"}</p>
        <p><strong>Dietary Preference:</strong> ${profile.dietary_preference || "N/A"}</p>
        <p><strong>Activity Status:</strong> ${profile.activity_status || "N/A"}</p>
    `;
}
