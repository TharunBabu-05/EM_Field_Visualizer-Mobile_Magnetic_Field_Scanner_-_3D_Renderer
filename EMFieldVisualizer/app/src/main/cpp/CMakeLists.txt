cmake_minimum_required(VERSION 3.22.1)

project("emfield_native")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add source files
add_library(
        emfield_native
        SHARED
        sensor_native.cpp
        kalman_filter.cpp
)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)

# Link libraries
target_link_libraries(
        emfield_native
        ${log-lib}
        ${android-lib}
)
