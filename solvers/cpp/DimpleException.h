#pragma once

#include <iostream>
#include <exception>
#include <string>
using namespace std;

class DimpleException : public exception
{
public:
	DimpleException(string message) : _message(message)
	{
	}
	~DimpleException() throw()
    {

    }
  

	virtual const char* what() const throw()
	{
		return _message.c_str();
	}

private:
	string _message;
};
