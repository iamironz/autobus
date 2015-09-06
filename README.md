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
    
License
-------
    The MIT License (MIT)
    
    Copyright (c) 2015 Alex Ironz
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
