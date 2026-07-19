import dis
import importlib.machinery
import importlib.util
import inspect
import sys
import types


def _load_module(module_name, module_path):
    lower_path = module_path.lower()

    if lower_path.endswith('.pyc'):
        loader = importlib.machinery.SourcelessFileLoader(module_name, module_path)
        spec = importlib.util.spec_from_loader(module_name, loader)
    elif lower_path.endswith(('.so', '.pyd', '.dylib')):
        loader = importlib.machinery.ExtensionFileLoader(module_name, module_path)
        spec = importlib.util.spec_from_file_location(module_name, module_path, loader=loader)
    else:
        raise ValueError('Expected a .pyc, .so, .pyd or .dylib file: ' + module_path)

    if spec is None or spec.loader is None:
        raise ImportError(f'Could not create module specification for {module_path}')

    previous = sys.modules.get(module_name)
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module

    try:
        spec.loader.exec_module(module)
    except BaseException:
        if previous is None:
            sys.modules.pop(module_name, None)
        else:
            sys.modules[module_name] = previous
        raise

    return module


def _safe_repr(value):
    try:
        return repr(value)
    except BaseException as exc:
        return f'<repr failed: {type(exc).__name__}: {exc}>'


def _safe_signature(value):
    try:
        return str(inspect.signature(value))
    except (TypeError, ValueError):
        return None


def _safe_doc(value):
    try:
        return inspect.getdoc(value)
    except BaseException:
        return None


def _safe_source(value):
    try:
        return inspect.getsource(value)
    except (OSError, TypeError, IOError):
        return None


def _annotations(value):
    result = {}
    for key, annotation in getattr(value, '__annotations__', {}).items():
        result[str(key)] = _safe_repr(annotation)
    return result


def _code_details(function):
    code = getattr(function, '__code__', None)
    if code is None:
        return None

    instructions = []
    try:
        for instruction in dis.get_instructions(function):
            instructions.append({
                'offset': instruction.offset,
                'opcode': instruction.opcode,
                'opname': instruction.opname,
                'arg': instruction.arg,
                'arg-value': _safe_repr(instruction.argval),
                'arg-repr': instruction.argrepr,
                'starts-line': instruction.starts_line,
                'jump-target?': instruction.is_jump_target,
            })
    except (TypeError, ValueError):
        instructions = []

    nested_code = []
    for constant in code.co_consts:
        if isinstance(constant, types.CodeType):
            nested_code.append({
                'name': constant.co_name,
                'qualified-name': getattr(constant, 'co_qualname', constant.co_name),
                'filename': constant.co_filename,
                'first-line-number': constant.co_firstlineno,
                'argument-count': constant.co_argcount,
                'variable-names': list(constant.co_varnames),
                'referenced-names': list(constant.co_names),
                'constants': [_safe_repr(item) for item in constant.co_consts],
            })

    return {
        'argument-count': code.co_argcount,
        'positional-only-argument-count': getattr(code, 'co_posonlyargcount', 0),
        'keyword-only-argument-count': code.co_kwonlyargcount,
        'local-variable-count': code.co_nlocals,
        'stack-size': code.co_stacksize,
        'flags': code.co_flags,
        'variable-names': list(code.co_varnames),
        'free-variables': list(code.co_freevars),
        'cell-variables': list(code.co_cellvars),
        'referenced-names': list(code.co_names),
        'constants': [_safe_repr(value) for value in code.co_consts],
        'filename': code.co_filename,
        'first-line-number': code.co_firstlineno,
        'instructions': instructions,
        'nested-code': nested_code,
    }


def _function_details(name, function):
    return {
        'name': name,
        'qualified-name': getattr(function, '__qualname__', None),
        'module': getattr(function, '__module__', None),
        'signature': _safe_signature(function),
        'annotations': _annotations(function),
        'doc': _safe_doc(function),
        'coroutine?': inspect.iscoroutinefunction(function),
        'async-generator?': inspect.isasyncgenfunction(function),
        'generator?': inspect.isgeneratorfunction(function),
        'builtin?': inspect.isbuiltin(function),
        'source': _safe_source(function),
        'code': _code_details(function),
    }


def _method_details(cls, name, member):
    raw = inspect.getattr_static(cls, name)
    static = isinstance(raw, staticmethod)
    class_method = isinstance(raw, classmethod)
    property_value = isinstance(raw, property)

    function = member
    if static or class_method:
        function = raw.__func__
    elif property_value:
        function = raw.fget

    result = _function_details(name, function) if function is not None else {
        'name': name,
        'qualified-name': None,
        'module': None,
        'signature': None,
        'annotations': {},
        'doc': _safe_doc(member),
        'coroutine?': False,
        'async-generator?': False,
        'generator?': False,
        'builtin?': False,
        'source': None,
        'code': None,
    }
    result.update({
        'static?': static,
        'classmethod?': class_method,
        'property?': property_value,
        'method-descriptor?': inspect.ismethoddescriptor(member),
    })
    return result


def _constant_details(name, value):
    return {
        'name': name,
        'type': type(value).__name__,
        'module': type(value).__module__,
        'value': _safe_repr(value),
    }


def _class_details(cls, include_inherited=False):
    methods = []
    attributes = []

    members = inspect.getmembers(cls) if include_inherited else list(cls.__dict__.items())
    for name, member in members:
        raw = inspect.getattr_static(cls, name)
        method_like = (
            inspect.isfunction(member)
            or inspect.ismethod(member)
            or inspect.isbuiltin(member)
            or inspect.ismethoddescriptor(member)
            or isinstance(raw, (staticmethod, classmethod, property))
        )

        if method_like:
            methods.append(_method_details(cls, name, member))
        elif not name.startswith('__'):
            attributes.append(_constant_details(name, member))

    return {
        'name': cls.__name__,
        'qualified-name': cls.__qualname__,
        'module': cls.__module__,
        'doc': _safe_doc(cls),
        'annotations': _annotations(cls),
        'bases': [
            {
                'name': base.__name__,
                'qualified-name': base.__qualname__,
                'module': base.__module__,
            }
            for base in cls.__bases__
        ],
        'method-resolution-order': [
            f'{base.__module__}.{base.__qualname__}'
            for base in inspect.getmro(cls)
        ],
        'methods': methods,
        'attributes': attributes,
        'source': _safe_source(cls),
    }


def inspect_compiled_module(module_name, module_path, include_inherited=False, include_imported=False):
    module = _load_module(module_name, module_path)
    functions = []
    classes = []
    constants = []

    for name, value in vars(module).items():
        if name.startswith('__'):
            continue

        defined_here = getattr(value, '__module__', module.__name__) == module.__name__
        if not include_imported and not defined_here and (inspect.isclass(value) or callable(value)):
            continue

        if inspect.isclass(value):
            classes.append(_class_details(value, include_inherited))
        elif inspect.isfunction(value) or inspect.isbuiltin(value):
            functions.append(_function_details(name, value))
        elif not inspect.ismodule(value):
            constants.append(_constant_details(name, value))

    return {
        'module': module.__name__,
        'module-file': getattr(module, '__file__', None),
        'doc': _safe_doc(module),
        'package': getattr(module, '__package__', None),
        'classes': classes,
        'functions': functions,
        'constants': constants,
        'source': _safe_source(module),
    }


def inspect_compiled_class(module_name, module_path, class_name, include_inherited=False):
    module = _load_module(module_name, module_path)
    cls = getattr(module, class_name)
    if not inspect.isclass(cls):
        raise TypeError(f'{class_name!r} is not a Python class')
    return _class_details(cls, include_inherited)
