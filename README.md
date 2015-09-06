# Autobus
Simple java representation of event bus mechanism

Using:
--------

**To subscribe class for events:**

    public void init() {
        Autobus.get().subscribe(this);
    }
    

**To create subscribers:**

    @Subscribe
    public void subscribeMethod(String string) {
        //do something with sent object
    }
    

**To send event for all subscribers:**

    Autobus.get().post("Hello, autobus!");
    

**To remove subscribers from broadcasting:**

    public void destroy() {
        Autobus.get().unsubscribe(this);
    }
    
