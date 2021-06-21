# AndroidApp

A calculator app for Android devices.

This app can recognize informally written equation, not demanding user to specify order of execution by explicitly placing open parentheses. In short, it can understand this:

3*(3log2+1)*sin3+2

without having the user specifically write this:

(3*((3log(2)+1)*sin(3)))+2

Also, there's an equation-solving mode, for those who are lazy. Just type in the equation, and the app would adjust its mode accordingly.

Some cool things:
- App controller receives display-ready responses from services and doesn't have to worry about exceptions, while services can throw as many exceptions as nescessary as exception handling is delegated to messages exchanged between services. (Meaning exceptions dissapear when the message is on its way to the service user)
- All services and controllers interact via actions (messages), irrespective of what that service's API is like (this is anonymity). Each service's function has its own action.
- If a service's API changes, there's only one place that needs to change and that is the use of that service in the respective action class. No need to re-write controller (service user).
- The action is performed by the time it is created, and the only thing left needs to do by the service user is to peek inside the message to get the result. (automation)
- All services needs to be Singleton to save memory since services are created inside messages.
