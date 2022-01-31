// Test case for issue 326:
// https://github.com/opprop/checker-framework-inference/issues/326

import ostrusted.qual.OsTrusted;

@SuppressWarnings("inconsistent.constructor.type")
// :: fixable-error: (declaration.inconsistent.with.extends.clause)
class A extends @OsTrusted Object {}

