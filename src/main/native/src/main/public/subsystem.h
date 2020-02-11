#pragma once

#include <string>
#include <memory>

namespace dolphin {
namespace subsystem {

struct ResouceConfig {
    std::string cpu_share;
    std::string cpu_set;
    std::string mem_limit;
};

class SubSystem {
public:
    virtual void set(const std::string& cgroup_path, std::shared_ptr<ResouceConfig> res) = 0;
    virtual void remove() = 0;
    virtual void apply() = 0;
    std::string name() { return name_; }
    SubSystem(const std::string& name) : name_(name) {}
private:
    std::string name_;
};

class CPUSubSystem : public SubSystem {
public:
    CPUSubSystem(const std::string& name) : SubSystem(name) {}
    void set(const std::string& cgroup_path, std::shared_ptr<ResouceConfig> res) override;
    void remove() override;
    void apply() override;
};

class CPUSetSubSystem : public SubSystem {
public:
    CPUSetSubSystem(const std::string& name) : SubSystem(name) {}
    void set(const std::string& cgroup_path, std::shared_ptr<ResouceConfig> res) override;
    void remove() override;
    void apply() override;
};

class MemSubSystem : public SubSystem {
public:
    MemSubSystem(const std::string& name) : SubSystem(name) {}
    void set(const std::string& cgroup_path, std::shared_ptr<ResouceConfig> res) override;
    void remove() override;
    void apply() override;
};


} // namespace subsystem
} // namespace dolphin