file(GLOB BLE_SRC
    "include/bleLibs/*.h"
    "include/bleLibs/*.cpp"
)

idf_component_register(SRCS "main.cpp"
                            ${BLE_SRC}
                    INCLUDE_DIRS ".")
