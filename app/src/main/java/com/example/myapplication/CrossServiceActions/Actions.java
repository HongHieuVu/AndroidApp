package com.example.myapplication.CrossServiceActions;

/**
 * The message services and controller send each other.
 * Exception handling and result formatting are delegated to this hierarchy tree in the subclasses.
 */
public abstract class Actions {
    private String result;
    MessageEffect postActionEffect;

    protected void setResult(MessageEffect messageEffect){
       result = messageEffect.send();
    }

    protected void setPostActions(MessageEffect messageEffect){
        messageEffect.send();
    }

    /**
     * gets result of the action
     * @return result string
     */
    public String getResult(){
        if (postActionEffect != null) setPostActions(postActionEffect);
        return result;
    }
}
