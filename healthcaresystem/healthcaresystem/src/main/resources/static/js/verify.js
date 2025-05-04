function fillEmail() {
    const params = new URLSearchParams(window.location.search);

    // Get the 'email' parameter
    const email = params.get("email");
    document.getElementById("email").value=email;
}

async function submitOTP(email, otp) {
    const res=await fetch("/api/verifyUser", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email,
            otp
        })
    });

    if(!res.ok) {
        console.log(await res.text());
        return;
    }

    const resData=await res.json();
    console.log(resData);
    if(resData.success) {
        localStorage.setItem("authToken", resData.token);
        localStorage.setItem("loggedIn", true);
        window.location.href="/main.html";
    }
}

window.addEventListener("DOMContentLoaded", ()=>{
    fillEmail();
    document.getElementById("verify-otp").addEventListener("click", e=>{
        e.preventDefault();
        const email=document.getElementById("email").value;
        const otp=document.getElementById("otp").value;
        
        submitOTP(email, otp);
    })
})