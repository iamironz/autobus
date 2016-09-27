# Autobus
Simple java representation of event bus mechanism

Using:
--------


**Getting started:**

First of all you must to determine what method should be event-based, you can make this by code snippet below:

```java
public class Test {


    @Subscribe
    public void onLoginSuccessEvent(LoginMeta meta) {

    }

}
```
    
After this step you should make your project. After making you'll be available `Autobus` class with strongly-typed methods `subscribe(Test)`, `unsubscribe(Test)` and `post(LoginMeta)` for `Test.java` only for this class. Methods for another classes and subscription methods will be generated and available after setting `@Subscribe` annotation and making project again.

**Create fabric for getting one instance of Autobus:**

```java
public class AutobusHelper {
    
    private static final Autobus autobus;
        
    static {
        autobus = new Autobus();
    }
        
    public static Autobus getAutobus() {
        return autobus;
    }
}
```

**To subscribe class for events:**

```java
public void init() {
    AutoBusHelper.getAutobus().subscribe(this);
}
```

**To send event for all subscribers:**

```java
AutoBusHelper.getAutobus().post("Hello, autobus!");   
```

**To remove subscribers from broadcasting:**

```java
public void destroy() {
    AutoBusHelper.getAutobus().unsubscribe(this);
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
