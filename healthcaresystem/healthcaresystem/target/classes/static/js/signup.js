async function signup(username, email, mobile, gender, password) {
    const res = await fetch("/api/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username,
            email,
            mobile,
            gender,
            password
        })
    });
    if (!res.ok) {
        console.log(await res.text());
        return;
    }
    const resData = await res.json();
    console.log(resData);
    window.location.href = "verify.html?email=" + resData.email;
}

window.addEventListener("DOMContentLoaded", () => {
    document.getElementById("send-otp").addEventListener("click", async () => {
        const username = document.getElementById("username").value;
        const email = document.getElementById("email").value;
        const mobile = document.getElementById("mobile").value;
        const gender = document.getElementById("gender").value;
        const password = document.getElementById("password").value;

        await signup(username, email, mobile, gender, password);
    })
})