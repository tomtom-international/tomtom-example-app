# Copyright 2026 TomTom International BV.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Car App Library - Keep all classes and suppress warnings
-keep class androidx.car.app.** { *; }
-dontwarn androidx.car.app.**

# Application package - Keep all classes
-keep class com.example.automotive.** { *; }

# Resource IDs - Keep all array and string resource IDs
-keep class **.R$array { *; }
-keep class **.R$string { *; }

