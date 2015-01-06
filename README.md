NoAnP
=====

Java Annotations and Annotation Processer for ensure override methods.

##Annotations

|Annotation|Overview|
|---|---|---|
|@Clone| if this annotation is added to a class, clone method should be overrided in the class and sub classes.|
|@MustOverride| if this annotation is added to a method, the method should be overrided in sub classes.|
|@SuppressCloneWarning| if this annotation is added to a class, the processer will skip the process of @Clone annotation check.|
|@SuppressOverrideWarning| if this annotation is added to a class, the processer will skip the process of @MustOverride annotation check.|

##Processer Option

Warning level is able to be changed by `nodamushi.override.level` option.Default level is 'warning'. Warning is not compile error.
If you want to fail to compile when threre are methods are not overrided, set the level as `error`. If you don't want to  any warning,set the level as `note`.

`nodamushi.override.level=error`

`nodamushi.override.level=note`



