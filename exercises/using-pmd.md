# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset. Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false positive). Explain why you would not solve this issue.

You can use the default [rule base](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/resources/rulesets/java/quickstart.xml) available on the source repository of PMD.

## Answer

Both the true positive and the false positive were taken from the [Apache Commons Collections](https://github.com/apache/commons-collections) source code.

### True positive

```
src/main/java/org/apache/commons/collections4/properties/OrderedProperties.java:164:    CompareObjectsWithEquals:    Use equals() to compare object references.
```

The problem is that it is always preferred to use .equals() when comparing objects (or in this case, references).

**File:** `OrderedProperties.java`
```java
sb.append(key == this ? "(this Map)" : key.toString());
```

Fixed line : 
```java
sb.append(key.equals(this) ? "(this Map)" : key.toString());
```

### False positive

A good false positive example is with the `SplitMapUtils` class, at line `75`.

**File:** `SplitMapUtils.java`
```java
@Override
public boolean equals(final Object arg0) {
    if (arg0 == this) {
        return true;
    }
    return arg0 instanceof WrappedGet && ((WrappedGet<?, ?>) arg0).get.equals(this.get);
}
```

Here, PMD shows a problem and presents a possible solution :
```
src\main\java\org\apache\commons\collections4\SplitMapUtils.java:75:    SimplifyBooleanReturns: This if statement can be replaced by `return {condition} || {elseBranch};`
```

This is a false positive because making that change would objectively make the code worse.

Here, the code uses the Return Early Pattern, leading to the code returning `true` *early* when it notices that the reference of the objected passed is equal to its own.