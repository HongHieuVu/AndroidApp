# AndroidApp

A calculator app for Android devices.

This app can recognize informally written equation, not demanding user to specify order of execution by explicitly placing open parentheses. In short, it can understand this:

3*(3log2+1)*sin3+2

without having the user specifically write this:

(3*((3log(2)+1)*sin(3)))+2

Also, there's an equation-solving mode, for those who are lazy. Just type in the equation, and the app would adjust its mode accordingly.

Some cool things:
- App controller receives display-ready responses from services and doesn't have to worry about exceptions, while services can throw as many exceptions as nescessary as exception handling is delegated to messages exchanged between services. (Meaning exceptions dissapear when the message is on its way to the service user). All exception handlings is in one place.
- Response formatting is done by the response on its way to the service user. Neither the service nor the user need to concern with output reformating.
- All services and controllers interact via actions (messages), irrespective of what that service's API is like (this is anonymity). Each service's function has its own associated action.
- All messages inherits from the action class, but the input variable types for each message can be different and there's no restriction in the number of inputs. This feature provides greater flexibility than overriding methods.
- If a service's API changes, there's only one place that needs to change and that is the use of that service in the respective action (message) class. No need to re-write controller (service user). All consequences of a service's API change is in one place, no matter how many classes use that API.
- The action is already performed by the time it is created, result is stored in the action, and the service user only needs to peek inside the message to get the result. (automation)
- All services needs to be Singleton to save memory.
- Actions can set other actions to be done after this action is done (set post-actions). (chaining actions is not recommended)
- There can be stand-alone actions that doesn't belong to any services (doesn't use any service API). This is permissible.

Finally, this app is multi-thread :D
