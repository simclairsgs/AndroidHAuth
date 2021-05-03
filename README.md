# AndroidHAuth

> This is android application built for Biometric user authentication using Android-API.
> 
> The Django backend is available at the repository <https://github.com/simclairsgs/UserAuthForHostel/>.
> 
> **Note: Read the instructions for backend first to understand clearly...**


> # Developer
> - George Simclair Sam ,<simclair.sgs@gmail.com>

---

> # How to use ?
> 
> - Connect to authentication network (in same network as server).
> - After Installing the app, get/create user account in django-admin(backend) and use the credentials to login.
> - After Successful login, the app will show current authentication status, in the allocated authentication slot the authenticate button will be enabled and on clicking it will take you to the biometric prompt, after successful authentication the data will be sent to server via API.


---

> # How to install ?
> - Download or clone repository and open it on android studio.
> - Change the TargetIP to your server IP Address and secret key in MainAtivity.java
> - Sync and Build the application.
> - Generate Apk file from android studio and install it on android devices.
> - **NOTE: The application requires Biometric prompt supported device and Android API level 28+**
> 

License type: BSD clause-3 License
