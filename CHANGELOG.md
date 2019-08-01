Version 2.0.2 *(2019-08-01)*
----------------------------
* Add support for preferred scrolling direction.

Version 2.0.1 *(2018-12-05)*
----------------------------
* Support android:text even when character list is not set in XML
* Support wraparound animation for going forwards as well as backwards

Version 2.0.0 *(2018-04-20)*
----------------------------
Please refer to the
[2.0 migration doc](https://github.com/robinhood/ticker/blob/master/2_0_migration.md)
for more information on this release.

Version 1.2.2 *(2017-12-11)*
----------------------------
* Fix bug where ticker column size doesn't change after text size change.
* Fix debounce bug when setting the same text on ticker.
* Support `tools:text`.

Version 1.2.1 *(2017-09-20)*
----------------------------
* Add support for `getText()`.
* Add support for `android:text` and `app:ticker_defaultCharacterList`.

Version 1.2.0 *(2017-04-28)*
----------------------------
* Add support for textStyle and text shadow attributes.
* Fix levenshtein bug that caused characters to disappear in animations.
* Update gradle dependencies

Version 1.1.1 *(2016-11-08)*
----------------------------
* Add minimum support for arbitrary characters.
* Update support and gradle dependencies.

Version 1.1.0 *(2016-08-11)*
----------------------------
* Performance optimizations for re-measure and re-layout.
* Better size change animation with Levenshtein algorithm.
* Added support for `animateMeasurementChange` to animate size changes smoothly during size change.
This will call `requestLayout()` on every animation frame so use with care.
* Added support for animator listener to listen to updates by the main animator.

Version 1.0.1 *(2016-07-25)*
----------------------------
* BREAKING: changed `app:ticker_textColor` into `android:textColor` and `app:ticker_textSize`
into `android:textSize`.
* Added support for getting/setting type face programmatically.
* Added support for `app:ticker_animationDuration`, `android:gravity`, `android:textAppearance`.


Version 1.0.0 *(2016-07-15)*
----------------------------

Initial release!
