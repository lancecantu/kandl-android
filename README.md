# kandl-android
Android frontend for Kandl grocery app

## Setup

`app/google-services.json` is not in the repo. The app is not distributed
publicly, so its Firebase config stays out of a public repo rather than
handing out the project id and API key.

To build, put your own there. Either download it from your Firebase project
(Project Settings, under Your apps), or copy `app/google-services.json.example`
and fill in real values. The build will not compile without it: the
google-services plugin generates `R.string.default_web_client_id` from that
file, and `LoginViewModel` uses it for Google sign-in.

Backend access is gated on a `kandlAccess` custom claim, so signing in to a
fresh Firebase project also means granting yourself that claim. See the
kandl-backend README.
