![](https://github.com/robinhood/ticker/blob/master/assets/ticker_main.gif)

What is Ticker?
===============

Ticker is a simple Android UI component for displaying scrolling text. Think about how an
odometer scrolls when going from one number to the next, that is similar to what Ticker does.
The Ticker handles smooth animations between strings and also string resizing (e.g. animate
from "9999" to "10000").

You can specify how the animations proceed by defining an array of characters in order. Each
character displayed by Ticker is controlled by this array which dictates how to animate from
a starting character to a target character. For example, if you just use a basic ASCII character
list, when animating from 'A' to 'Z', it will go from 'A' -> 'B' -> ... 'Z'. The character
ordering does not wrap around, meaning that to animate from 'Z' to 'A' it will go from
'Z' -> 'Y' -> ... -> 'A'.


Getting started
---------------

Add the ticker dependency to your `build.gradle`.

```groovy
compile 'com.robinhood.ticker:ticker:1.2.1'
```


Usage
-----

Define the `TickerView` in XML:

```xml
<com.robinhood.ticker.TickerView
    android:id="@+id/tickerView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

Then add the character array to specify the animation style:

```java
final TickerView tickerView = findViewById(R.id.tickerView);
tickerView.setCharacterList(TickerUtils.getDefaultNumberList());
```

That's it! Now you can call `setText` to display your data.


Customization
-------------

We currently support a fairly limited subset of customizations at the moment so please let us
know what new features / APIs you need exposed for your use-case and we'll consider it for
future releases (or of course feel free to fork!).

You can customize the looks and feels of the `TickerView` via XML:

```xml
android:gravity="center"
android:textColor="@color/colorPrimary"
android:textSize="16sp"
app:ticker_animationDuration="1500"
```

Or Java:

```java
tickerView.setTextColor(textColor);
tickerView.setTextSize(textSize);
tickerView.setTypeface(myCustomTypeface);
tickerView.setAnimationDuration(500);
tickerView.setAnimationInterpolator(new OvershootInterpolator());
tickerView.setGravity(Gravity.START);
```

For the full list of XML attributes that we support, please refer to the 
[attrs](https://github.com/robinhood/ticker/blob/master/ticker/src/main/res/values/attrs.xml) file.


Performance
-----------

We decided to extend from the base `View` class and achieve everything by drawing directly
onto the canvas. The primary benefit from this is having full flexibility and control over
memory allocations and minimize performance impact by using native draw operations. We
pre-allocate and pre-compute as much as possible for each transition so that the only thing
we need to do in the draw path is perform the actual drawing operations. The performance test
UI included in the [ticker-sample](https://github.com/robinhood/ticker/tree/master/ticker-sample)
is a bit over-zealous but animates smoothly with a screen full of tickers.


License
=======

    Copyright 2016 Robinhood Markets, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
