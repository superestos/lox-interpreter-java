package com.superestos;

import java.util.List;

interface Callable {
    Object call(Interpreter interpreter, List<Object> arguments);
    int arity();
}
