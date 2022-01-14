package com.example.myapplication.CrossServiceActions;

/**
 * The message services and controller send each other.
 * Exception handling and result formatting are delegated to this hierarchy tree in the subclasses.
 * This set up allows service users to treat all services' messages in the same way. A service user
 * could be another service.
 * Actions will be programmed as the subclass is created.
 * Each message is independent and eventhough all messages inherit this class, its
 * content is private.
 */
public abstract class Actions {
    private String result;
    protected MessageEffect postActionEffect;

    /**
     * Programs what the message will do. Functional interface is used to restrict
     * overriding this method in order to prevent subclasses from loosening access to the message's
     * content and to provide greater flexibility in input variables types.
     * @param messageEffect effect to be provided
     */
    final void setResult(MessageEffect messageEffect){
       result = messageEffect.send();
    }

    protected void setPostAction(MessageEffect messageEffect){
        postActionEffect = messageEffect;
    }

    private void doPostActions(){
        postActionEffect.send();
    }

    /**
     * gets result of the action
     * @return result string
     */
    public String getResult(){
        if (postActionEffect != null) doPostActions();
        return result;
    }
}
