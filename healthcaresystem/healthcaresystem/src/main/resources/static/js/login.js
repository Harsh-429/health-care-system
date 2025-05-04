async function login(email, password) {
    const res=await fetch("/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email,
            password
        })
    });
    if(!res.ok) {
        console.log(await res.text());
        return;
    }

    const resData=await res.json();
    localStorage.setItem("authToken", resData.token);
    localStorage.setItem("loggedIn", true);
    window.location.href="/main.html";
}

window.addEventListener('DOMContentLoaded', ()=>{
    document.getElementById("submit-btn").addEventListener("click", async e=>{
        const email=document.getElementById("username").value;
        const password=document.getElementById("password").value;

        await login(email, password);
    })
})