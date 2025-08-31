package dev.mr2.dpc;

import android.os.Bundle;

interface IUserService {
    Bundle execute(String command) = 1;
    void destroy() = 16777114;
}
