/**
 * Created by Yun on 2015-12-12.
 */
import {NativeModules, NativeAppEventEmitter} from 'react-native';

const {QQAPI} = NativeModules;


export function login(scopes) {
    return QQAPI.login(scopes);
}

export function isQQInstalled() {
    return QQAPI.isQQInstalled();
}

export function shareToQQ(data={}) {
    return QQAPI.shareToQQ(data);
}

export function shareToQzone(data={}) {
    return QQAPI.shareToQzone(data);
}

export function logout(){
    QQAPI.logout();
}




