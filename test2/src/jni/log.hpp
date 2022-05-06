//
// Created by Ðìçü on 2021/6/2.
//

#ifndef LIFESEARCH_LOG_HPP
#define LIFESEARCH_LOG_HPP

#include <android/log.h>

#define debug(...) __android_log_print(ANDROID_LOG_DEBUG, "Test", __VA_ARGS__)

#endif //LIFESEARCH_LOG_HPP
