#include <stdio.h>

class MyClass {
    public:
        int num;
        // String str;
};

extern "C" void app_main(void)
{
    printf("\n\n\n\nHello there from C++\n");

    MyClass obj;

    obj.num = 12;
    // obj.str = "WOAH";

    printf("\n%d \n", obj.num);

}
