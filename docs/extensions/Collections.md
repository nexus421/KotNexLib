### Collection Extensions

Enhanced operations for `Iterable`, `List`, `Set`, and `Map`.

#### Iterable & List

- `forEachDoLast { item, isLast -> ... }`: Iterate and easily detect the last element.
- `splitFilter { predicate }`: Splits a collection into two lists based on a condition.
- `handleSizes()`: A sealed class approach (`IsEmpty`, `IsOne`, `HasMany`) to handle collection sizes in `when`
  expressions.
- `move(element, toIndex)`: Moves an item within a `MutableList`.
- `isBefore(item, condition)` / `isAfter(item, condition)`: Checks the relative order of elements.

#### Map & Set

- `contains(predicate)`: Search for elements using a predicate.
- Various utility extensions to simplify common tasks.
