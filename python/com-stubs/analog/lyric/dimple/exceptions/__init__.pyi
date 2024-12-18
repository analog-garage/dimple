
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import java.lang
import typing



class DimpleException(java.lang.RuntimeException):
    @typing.overload
    def __init__(self, exception: java.lang.Exception): ...
    @typing.overload
    def __init__(self, string: typing.Union[java.lang.String, str]): ...
    @typing.overload
    def __init__(self, string: typing.Union[java.lang.String, str], *object: typing.Any): ...
    @typing.overload
    def __init__(self, throwable: java.lang.Throwable, string: typing.Union[java.lang.String, str], *object: typing.Any): ...
    @staticmethod
    def unimplementedMethod(class_: typing.Type[typing.Any], string: typing.Union[java.lang.String, str]) -> 'DimpleException': ...
    @staticmethod
    def unsupported(string: typing.Union[java.lang.String, str]) -> 'DimpleException': ...
    @staticmethod
    def unsupportedBySolver(string: typing.Union[java.lang.String, str], string2: typing.Union[java.lang.String, str]) -> 'DimpleException': ...
    @typing.overload
    @staticmethod
    def unsupportedMethod(class_: typing.Type[typing.Any], string: typing.Union[java.lang.String, str]) -> 'DimpleException': ...
    @typing.overload
    @staticmethod
    def unsupportedMethod(class_: typing.Type[typing.Any], string: typing.Union[java.lang.String, str], string2: typing.Union[java.lang.String, str]) -> 'DimpleException': ...

class IDimpleException:
    def getException(self) -> java.lang.Exception: ...

class DomainException(DimpleException):
    def __init__(self, string: typing.Union[java.lang.String, str], *object: typing.Any): ...

class InvalidDistributionException(DimpleException):
    def __init__(self, string: typing.Union[java.lang.String, str], *object: typing.Any): ...

class NormalizationException(InvalidDistributionException):
    def __init__(self, string: typing.Union[java.lang.String, str], *object: typing.Any): ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.exceptions")``.

    DimpleException: typing.Type[DimpleException]
    DomainException: typing.Type[DomainException]
    IDimpleException: typing.Type[IDimpleException]
    InvalidDistributionException: typing.Type[InvalidDistributionException]
    NormalizationException: typing.Type[NormalizationException]
