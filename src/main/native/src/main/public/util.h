#pragma once

#include <iostream>
#include <string>
#include <fstream>
#include <sys/stat.h>
#include <sys/types.h>

namespace dolphin {
namespace util {

int create_dir(const std::string& path) {
    return mkdir(path.c_str(), S_IRUSR | S_IWUSR | S_IXUSR | S_IRWXG | S_IRWXO);
}

const std::string find_cgroup_path(const std::string& subsystem_name) {
    std::ifstream subsystem(subsystem_name);
    if (!subsystem) {
        std::cerr << "subsystem is not exist!" << std::endl;
        return "";
    }

    subsystem.close();
}

} // namespace util
} // namespace dolphin