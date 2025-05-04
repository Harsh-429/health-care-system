document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('submitBtn').addEventListener('click', function () {
        // Retrieve auth token from localStorage
        const authToken = localStorage.getItem('authToken');

        if (!authToken) {
            alert("Authorization token is missing! Please log in again.");
            return;
        }

        // Construct form data
        const formData = {
            age: document.getElementById('age').value ? parseInt(document.getElementById('age').value) : null,
            gender: document.getElementById('gender').value || null,
            bloodGroup: document.getElementById('blood_group').value || null,
            heightCm: document.getElementById('height_cm').value ? parseFloat(document.getElementById('height_cm').value) : null,
            weightKg: document.getElementById('weight_kg').value ? parseFloat(document.getElementById('weight_kg').value) : null,
            exercisePlan: document.getElementById('exercise_plan').value || null,
            dietaryPreference: document.getElementById('dietary_preference').value || null
        };

        console.log("Sending Data:", formData);

        // Send request with Authorization header
        fetch('http://localhost:8080/user/updateProfile', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(formData)
        })
            .then(response => response.json())
            .then(data => {
                console.log("Response:", data);
                if (data.status === "success") {
                    alert("Profile updated successfully!");
                    window.location.href="profile.html";
                } else {
                    alert("Error: " + data.message);
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Failed to update profile. Please try again.");
            });
    });

})