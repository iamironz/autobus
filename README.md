# Autobus
Simple java and android representation of event bus mechanism based on annotation processing.
This approach not increase android dex count and not needed to use a lot of bloat library code.


Adding:
------------------
For using in your own projects just add jcenter repository superset:

```java
repositories {
    jcenter()
}
```

And after you should just add this dependencies:

```java
apt 'com.implimentz:autobus-compiler:0.0.5'
compile 'com.implimentz:autobus:0.0.5'
```

Using:
------------------


**Getting started:**

First of all you must to determine what method should be event-based, you can make this by code snippet below:

```java
public class Subscriber {
    @Subscribe
    public final void onLoginSuccessEvent(LoginMetaEvent event) {
        //some logic here
    }
}
```
Please note that method must be only `public`, `final` and should no return any value (must be `void`). Also number of arguments must be as 1 and argument class type should be named by \*Event suffix pattern.
    
After this step you should make your project. After making you'll be available `Autobus` class with strongly-typed static methods `subscribe(Subscriber)`, `unsubscribe(Subscriber)` and `post(LoginMeta)` for `Subscriber.java` class only. Methods for another classes and subscription methods will be generated and available after setting `@Subscribe` annotation and making project again.


**To subscribe class for events:**

```java
public class Subscriber {
    public void init() {
        Autobus.subscribe(this);
    }
}
```

**To send event for all subscribers:**

```java
public class Sender {
    public void sendSuccess() {
        Autobus.post(new LoginMeta());   
    }
}
```

**To remove subscribers from broadcasting:**

```java
public class Subscriber {
    public void destroy() {
        Autobus.unsubscribe(this);
    }
}
```
    
License
-------
    Copyright 2016 Alexander Efremenkov
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
