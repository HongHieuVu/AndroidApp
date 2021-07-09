package com.example.myapplication.CrossServiceActions;

/**
 * The message services and controller send each other.
 * Exception handling and result formatting are delegated to this hierarchy tree in the subclasses.
 */
public abstract class Actions {
    String result;
    protected MessageEffect postActionEffect;

    protected void setResult(MessageEffect messageEffect){
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
